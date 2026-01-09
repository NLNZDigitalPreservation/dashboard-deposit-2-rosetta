from app.data.models import db_manager, PermanentIndex, GlobalSettings


def test_db_manager(peewee_db):
    g_settings = GlobalSettings(id=None)
    g_settings.save(force_insert=True)
    new_settings = GlobalSettings.get_or_none(id=g_settings.id)
    assert new_settings is not None
