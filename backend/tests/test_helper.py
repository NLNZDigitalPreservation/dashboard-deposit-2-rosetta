import time

import pytest

from common.utils.helper import format_bytes, format_rate, is_empty_str


@pytest.mark.parametrize(
    "value, expected",
    [
        (None, True),
        ("", True),
        ("   ", True),
        ("abc", False),
    ],
)
def test_is_empty_str(value, expected):
    assert is_empty_str(value) is expected


@pytest.mark.parametrize(
    "value, expected",
    [
        (0, "0.00B"),
        (1023, "1023.00B"),
        (1024, "1.00KB"),
        (1024 * 1024, "1.00MB"),
    ],
)
def test_format_bytes(value, expected):
    assert format_bytes(value) == expected


def test_format_rate_handles_zero_elapsed_seconds():
    assert format_rate(1234, 0) == "N/A"


def test_format_rate_formats_bytes_per_second():
    assert format_rate(2048, 2) == "1.00KB/s"
