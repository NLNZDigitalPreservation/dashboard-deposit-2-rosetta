import logging
from dataclasses import dataclass

from ldap3 import ALL, NTLM, Connection, Server
from ldap3.utils.dn import parse_dn

from app.auth.sessions import UserInfo


class LDAPAuthentication:
    def __init__(self, args):
        self.args = args
        self.server = Server(args.ldap_url, get_info=ALL)

    def authenticate(self, user_info: UserInfo, password_input):
        username_input = user_info.user_name

        manager_conn = Connection(
            self.server,
            user=self.args.ldap_contextsource_manager_dn,
            password=self.args.ldap_contextsource_manager_password,
        )
        if not manager_conn.bind():
            err = f"LDAP Manager Bind Failed: {manager_conn.result}"
            logging.error(err)
            return False, err

        # Format the filter with the actual username input (e.g., '(uid=jdoe)')
        actual_filter = self.args.ldap_usrsearchfilter.format(username_input)

        # We only need the DN to verify the user
        manager_conn.search(
            search_base=self.args.ldap_usrsearchbase,
            search_filter=actual_filter,
            attributes=[
                "cn",
                "mail",
                "userPrincipalName",
                "sAMAccountName",
                "thumbnailPhoto",
                "jpegPhoto",
            ],
        )

        # Check if we found exactly one user
        if len(manager_conn.entries) == 0:
            err = "User not found."
            logging.error(err)
            manager_conn.unbind()
            return False, err

        # Extract the user's full DN (Distinguished Name)
        user_full_dn = manager_conn.entries[0].entry_dn
        logging.debug(f"Found User DN: {user_full_dn}")

        entry_attrs = manager_conn.entries[0].entry_attributes_as_dict
        user_info.email = (
            entry_attrs.get("mail")[0] if entry_attrs.get("mail") else None
        )
        user_info.presentation_name = (
            entry_attrs.get("cn")[0] if entry_attrs.get("cn") else None
        )
        if not user_info.email or not user_info.presentation_name:
            return False, "Not able to get users profile"

        # We are done with the manager connection
        manager_conn.unbind()

        # --- STEP 3: Bind as the User ---
        # Now we verify the user's password using the DN we just found
        user_conn = Connection(self.server, user=user_full_dn, password=password_input)

        if user_conn.bind():
            logging.debug("User authentication SUCCESS.")
            user_conn.unbind()
            # user.save()
            return True, user_info
        else:
            err = "User authentication FAILED (Wrong Password)."
            logging.error(err)
            return False, err
