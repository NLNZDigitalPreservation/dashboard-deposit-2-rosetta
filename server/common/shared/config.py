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

    def add_ldap_arguments(self):
        self.add_env_argument(
            "--ldap-enabled",
            type=str2bool,
            default="true",
            help="Enable or disable LDAP authentication",
        )

        self.add_env_argument(
            "--ldap-url",
            default="ldap://wlgprddc15.dia.govt.nz:389",
            help="The LDAP server URI",
        )

        self.add_env_argument(
            "--ldap-usrsearchbase",
            default="dc=dia,dc=govt,dc=nz",
            help="Base DN for user search",
        )

        self.add_env_argument(
            "--ldap-usrsearchfilter",
            default="(sAMAccountName={})",
            help="LDAP search filter for users",
        )

        self.add_env_argument(
            "--ldap-groupsearchbase",
            default="",
            help="Base DN for group search",
        )

        self.add_env_argument(
            "--ldap-groupsearchfilter",
            default="",
            help="LDAP search filter for groups",
        )

        self.add_env_argument(
            "--ldap-contextsource-root",
            default="",
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

    def add_app_main_arguments(self, api_port):
        self.add_env_argument(
            "--test-mode",
            default=True,
            help="The flag for testing purpose",
        )

        self.add_env_argument(
            "--persistent-storage",
            default="/exlibris/dps/nlnz_tools/dashboard/running_data",
            help="The persistent directory for the running data",
        )

        # Rosetta access
        self.add_env_argument(
            "--rosetta-dps-url",
            type=str,
            default="https://wlguatdpsilb.natlib.govt.nz/rest/v0",
            help="Rosetta REST API DPS URL",
        )
        self.add_env_argument(
            "--rosetta-sip-url",
            type=str,
            default="https://wlguatoprilb.natlib.govt.nz/rest/v0",
            help="Rosetta REST API SIP URL",
        )

        # Process & Job Intervals
        self.add_env_argument(
            "--process-scan-interval",
            type=int,
            default=3600,
            help="Scan interval for process settings in seconds",
        )
        self.add_env_argument(
            "--deposit-job-scan-interval",
            type=int,
            default=60,
            help="Scan interval for deposit jobs in seconds",
        )

    @staticmethod
    def parse_duration(duration: str) -> int:
        """Parses a duration string in the format XdXhXm to seconds."""

        # Convert the journal max age to seconds
        match = re.match(r"^(\d+)d(\d+)h(\d+)m$", duration)
        if match is None:
            raise ValueError("Invalid duration format, must be in the format XdXhXm. " f"Instead, {duration} was given.")
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
