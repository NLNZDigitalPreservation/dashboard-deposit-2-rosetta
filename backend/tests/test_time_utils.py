import time

from common.shared.time_utils import Timeout, Timer, named_timer


def test_timeout_stops_iteration_and_can_be_suppressed():
    timeout = Timeout(0.0, raise_error=False)

    assert bool(timeout) is False


def test_timer_records_elapsed_and_rolling_average():
    timer = Timer(name="sample")

    with timer:
        time.sleep(0.01)

    assert timer.elapsed >= 0.0
    assert timer.total_elapsed > 0.0
    assert timer.average > 0.0
    assert timer.average_fps > 0.0


def test_named_timer_reuses_instances():
    first = named_timer("shared")
    second = named_timer("shared")

    assert first is second
