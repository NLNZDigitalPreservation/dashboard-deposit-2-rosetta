import logging
from ldap3 import Server, Connection, ALL, NTLM
from ldap3.core.exceptions import LDAPException


class LDAPAuthentication:
    def __init__(self, args):
        self.args = args
        self.ldap_enabled = args.ldap_enabled
        self.server = Server(args.ldap_url, get_info=ALL)

    def authenticate(self, username_input, password_input):
        if not self.ldap_enabled:
            return True, "SKIP"  # LDAP is disabled; allow all authentications

        manager_conn = Connection(self.server, user=self.args.ldap_contextsource_manager_dn, password=self.args.ldap_contextsource_manager_password)
        if not manager_conn.bind():
            err = f"LDAP Manager Bind Failed: {manager_conn.result}"
            logging.error(err)
            return False, err

        # Format the filter with the actual username input (e.g., '(uid=jdoe)')
        actual_filter = self.args.ldap_usrsearchfilter.format(username_input)

        # We only need the DN to verify the user
        manager_conn.search(search_base=self.args.ldap_usrsearchbase, search_filter=actual_filter, attributes=["cn"])

        # Check if we found exactly one user
        if len(manager_conn.entries) != 1:
            err = "User not found or multiple users found."
            logging.error(err)
            manager_conn.unbind()
            return False, err

        # Extract the user's full DN (Distinguished Name)
        user_full_dn = manager_conn.entries[0].entry_dn
        logging.debug(f"Found User DN: {user_full_dn}")

        # We are done with the manager connection
        manager_conn.unbind()

        # --- STEP 3: Bind as the User ---
        # Now we verify the user's password using the DN we just found
        user_conn = Connection(self.server, user=user_full_dn, password=password_input)

        if user_conn.bind():
            logging.debug("User authentication SUCCESS.")
            user_conn.unbind()
            return True, "OK"
        else:
            err = "User authentication FAILED (Wrong Password)."
            logging.error(err)
            return False, err
