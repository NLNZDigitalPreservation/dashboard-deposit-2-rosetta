from datetime import date, datetime


def _json_safe_value(value):
    if isinstance(value, (datetime, date)):
        return value.isoformat()
    if isinstance(value, dict):
        return {k: _json_safe_value(v) for k, v in value.items()}
    if isinstance(value, (list, tuple, set)):
        return [_json_safe_value(v) for v in value]
    return value


def format_pagination_results(query, paginator, order_by):
    total_count = query.count()
    page = int(paginator.offset / paginator.rows) + 1
    datasets = query.order_by(order_by).paginate(page, paginator.rows).dicts()

    results = {"total_records": total_count, "data": _json_safe_value(list(datasets))}
    return results
