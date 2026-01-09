import dataclasses
from typing import Any, Type


def get_field_names(cls: Type[Any]) -> list[str]:
    if not dataclasses.is_dataclass(cls):
        raise TypeError("Expected a dataclass type")
    return [f.name for f in dataclasses.fields(cls)]


def to_camel_case(snake_str):
    components = snake_str.split("_")
    # We join them such that the first component is lowercase
    # and subsequent ones are capitalized.
    return components[0] + "".join(x.title() for x in components[1:])


def camel_case_dict_factory(data):
    """
    Transform snake_case keys to camelCase keys during asdict() conversion.
    """
    return {to_camel_case(k): v for k, v in data}


def dataclass_as_dict(instance):
    """
    Convert a dataclass instance to a dictionary with camelCase keys.
    """
    return dataclasses.asdict(instance)


def dataclass_as_camel_dict(instance):
    """
    Convert a dataclass instance to a dictionary with camelCase keys.
    """
    return dataclasses.asdict(instance, dict_factory=camel_case_dict_factory)


def dict_as_camel_dict(instance):
    """
    Convert a dictionary to a dictionary with camelCase keys.
    """
    return {to_camel_case(k): v for k, v in instance.items()}
