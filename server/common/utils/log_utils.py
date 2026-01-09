import logging
import sys
from typing import Optional


def init(log_level: Optional[str] = None):
    format_string = "{asctime} {levelname:<5.5} {filename: <32.32} {lineno:>4}: [{process}] {message}"
    log_format = logging.Formatter(format_string, style="{")
    log_level = "INFO" if log_level is None else log_level.upper()
    logger = logging.getLogger()
    logger.setLevel(log_level)
    if logger.handlers is not None and len(logger.handlers) > 0:
        console_handler = logger.handlers[0]
        console_handler.setFormatter(log_format)
    else:
        console_handler = logging.StreamHandler(sys.stdout)
        console_handler.setFormatter(log_format)
        logger.addHandler(console_handler)
