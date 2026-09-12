import os
import re
from argparse import ArgumentParser


def str2bool(value):
    true_values = {"true", "1", "yes", "y", "t", "on"}
    false_values = {"false", "0", "no", "n", "f", "off"}

    value = value.strip().lower()
    if value in true_values:
        return True
    elif value in false_values:
        return False
    else:
        raise ValueError(f"Invalid truth value: {value}")


class Parser(ArgumentParser):
    """An easy way to make flags also configurable from env variables"""

    def add_env_argument(self, *args, **kwargs):
        if len(args) != 1:
            raise ValueError("Provide exactly one flag name")

        flag_name = args[0]

        # Prioritize environment variables over the normal default
        environment_default = os.environ.get(_to_env_var_name(flag_name), None)
        if environment_default is not None:
            # If a type is specified such as 'int' or 'float' or 'str', convert
            # the environment variable using the callback
            type_callback = kwargs.get("type", None)
            if type_callback:
                environment_default = type_callback(environment_default)

            kwargs["default"] = environment_default

        # Mark the flag as not required if it's supplied as an env variable
        no_env_variable_set = _to_env_var_name(flag_name) not in os.environ
        marked_required = kwargs.get("required", False)
        kwargs["required"] = marked_required and no_env_variable_set

        super().add_argument(flag_name, **kwargs)

    def add_log_level(self):
        self.add_env_argument(
            "--log-level",
            default="INFO",
            help="The log level of logging",
        )
        self.add_env_argument(
            "--log-file",
            default="/data/viridian/persistent/dashboard/logs/dashboard.log",
            help="The file to write logs to (defaults to stdout if not set)",
        )

    def add_dbms_arguments(self):
        """Adds arguments required for any service that connects to the DBMS"""
        self.add_env_argument(
            "--dbms-hostname",
            default="localhost",
            help="The hostname of the DBMS",
        )
        self.add_env_argument(
            "--dbms-port",
            default=5432,
            help="The hostname of the DBMS",
        )
        self.add_env_argument(
            "--dbms-username",
            default="fixity",
            help="The username to connect to the DBMS with",
        )
        self.add_env_argument(
            "--dbms-password",
            default="fixity",
            help="The password to connect to the DBMS with",
        )
        self.add_env_argument(
            "--db-name",
            default="fixity",
            help="The name of the database in the DBMS to connect to",
        )
        self.add_env_argument(
            "--optional_db_tables",
            required=False,
            help="enable or disable OPTIONAL DB TABLES",
        )

    def add_ldap_arguments(self):
        self.add_env_argument(
            "--ldap-enabled",
            type=str2bool,
            default=False,
            help="Enable or disable LDAP authentication",
        )

        self.add_env_argument(
            "--ldap-url",
            default="ldap://yourldapserver.domain.com:389/",
            help="The LDAP server URI",
        )

        self.add_env_argument(
            "--ldap-usrsearchbase",
            default="ou=people",
            help="Base DN for user search",
        )

        self.add_env_argument(
            "--ldap-usrsearchfilter",
            default="(uid={0})",
            help="LDAP search filter for users",
        )

        self.add_env_argument(
            "--ldap-groupsearchbase",
            default="ou=groups",
            help="Base DN for group search",
        )

        self.add_env_argument(
            "--ldap-groupsearchfilter",
            default="(member={0})",
            help="LDAP search filter for groups",
        )

        self.add_env_argument(
            "--ldap-contextsource-root",
            default="dc=com",
            help="LDAP Context Source Root DN",
        )

        self.add_env_argument(
            "--ldap-contextsource-manager-dn",
            default="",
            help="Manager DN used for LDAP binds",
        )

        self.add_env_argument(
            "--ldap-contextsource-manager-password",
            default="",
            help="Password for manager DN",
        )

    def add_app_arguments(self, api_port, base_dir):
        self.add_env_argument(
            "--app-deployment",
            default="DEV",
            help="The deployment environment",
        )
        self.add_env_argument(
            "--app-version",
            default="@app.version@",
            help="The application version",
        )
        self.add_env_argument(
            "--user-institution",
            default="INS00",
            help="The institution code",
        )
        self.add_env_argument(
            "--rosetta-rest-api-dps-url",
            default="https://wlguatdpsilb.natlib.govt.nz/rest/v0",
            help="The Rosetta DPS REST API URL",
        )
        self.add_env_argument(
            "--rosetta-rest-api-sip-url",
            default="https://wlguatoprilb.natlib.govt.nz/rest/v0",
            help="The Rosetta SIP REST API URL",
        )
        self.add_env_argument(
            "--ldap-enable",
            type=str2bool,
            default=False,
            help="Enable or disable LDAP authentication",
        )
        self.add_env_argument(
            "--process-setting-scan-interval",
            default=3600,
            type=int,
            help="The scan interval for process settings in seconds",
        )
        self.add_env_argument(
            "--deposit-job-scan-interval",
            default=60,
            type=int,
            help="The scan interval for deposit jobs in seconds",
        )
        self.add_env_argument(
            "--management-endpoints-web-exposure-include",
            default="health,info,loggers",
            help="The exposed management endpoints",
        )
        self.add_env_argument(
            "--logging-path",
            default="/exlibris/dps/nlnz_tools/dashboard/logs/",
            help="The directory for application logs",
        )

    def add_email_arguments(self):
        self.add_env_argument(
            "--mail-enabled",
            type=str2bool,
            default="false",
            help="Enable or disable email notifications",
        )
        self.add_env_argument(
            "--mail-protocol",
            default="SMTP",
            help="Mail transport protocol",
        )

        self.add_env_argument(
            "--mail-smtp-host",
            default="localhost",
            help="SMTP server hostname",
        )

        self.add_env_argument(
            "--mail-smtp-port",
            type=int,
            default=25,
            help="SMTP server port",
        )

    @staticmethod
    def parse_duration(duration: str) -> int:
        """Parses a duration string in the format XdXhXm to seconds."""

        # Convert the journal max age to seconds
        match = re.match(r"^(\d+)d(\d+)h(\d+)m$", duration)
        if match is None:
            raise ValueError(
                "Invalid duration format, must be in the format XdXhXm. "
                f"Instead, {duration} was given."
            )
        duration_seconds = int(match[1]) * 86400  # Convert days to seconds
        duration_seconds += int(match[2]) * 3600  # Convert hours to seconds
        duration_seconds += int(match[3]) * 60  # Convert minutes to seconds

        return duration_seconds

    @staticmethod
    def str_to_bool(string: str) -> bool:
        return str2bool(string)


def _to_env_var_name(flag_name: str) -> str:
    """Converts a flag to an idiomatic environment variable name"""
    return flag_name.replace("--", "").replace("-", "_").upper()


def parse_args(api_port, base_dir):
    parser = Parser(description="This is the Deposit Dashboard")

    parser.add_app_arguments(api_port=api_port, base_dir=base_dir)
    parser.add_log_level()
    parser.add_dbms_arguments()
    parser.add_ldap_arguments()
    parser.add_email_arguments()
    parser.add_app_arguments(api_port=api_port, base_dir=base_dir)

    args_namespace = parser.parse_known_args()
    args = args_namespace[0]

    return args
