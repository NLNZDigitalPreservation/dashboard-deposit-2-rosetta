from app.auth.ldap_client import LDAPAuthentication


def test_ldap_connection(env_args):
    env_args.ldap_enabled = True
    auth = LDAPAuthentication(env_args)

    result, err = auth.authenticate("bootstrap", "password")
    assert not result, f"LDAP authentication failed: {err}"

    result, err = auth.authenticate("leefr", "password")
    assert not result, f"LDAP authentication failed: {err}"
