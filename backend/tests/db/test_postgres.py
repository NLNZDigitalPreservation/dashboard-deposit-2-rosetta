import logging

import pytest
from common.data.models import PermanentIndex


def test_postgres_connection(peewee_db):

    try:
        count = PermanentIndex.select().count()
        assert count is not None, "Failed to retrieve count from Postgres database."
        logging.info(f"Successfully retrieved count from Postgres database: {count}")
    except Exception as e:
        pytest.fail(f"Postgres database connection failed: {e}")


def test_group_by(peewee_db):
    query = (
        PermanentIndex.select(PermanentIndex.index_location)
        .group_by(PermanentIndex.index_location)
        .tuples()
    )
    locations = [index_location for (index_location,) in query]
    assert len(locations) == len(set(locations))
