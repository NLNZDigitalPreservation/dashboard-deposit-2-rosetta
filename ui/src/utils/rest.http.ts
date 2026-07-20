export const HttpStatus = {
    // 1xx - Informational
    100: 'Continue',
    101: 'Switching Protocols',
    102: 'Processing',
    103: 'Early Hints',

    // 2xx - Success
    200: 'OK',
    201: 'Created',
    202: 'Accepted',
    203: 'Non-Authoritative Information',
    204: 'No Content',
    205: 'Reset Content',
    206: 'Partial Content',
    207: 'Multi-Status',
    208: 'Already Reported',
    226: 'IM Used',

    // 3xx - Redirection
    300: 'Multiple Choices',
    301: 'Moved Permanently',
    302: 'Found',
    303: 'See Other',
    304: 'Not Modified',
    307: 'Temporary Redirect',
    308: 'Permanent Redirect',

    // 4xx - Client Error
    400: 'Bad Request',
    401: 'Unauthorized',
    402: 'Payment Required',
    403: 'Forbidden',
    404: 'Not Found',
    405: 'Method Not Allowed',
    406: 'Not Acceptable',
    407: 'Proxy Authentication Required',
    408: 'Request Timeout',
    409: 'Conflict',
    410: 'Gone',
    411: 'Length Required',
    412: 'Precondition Failed',
    413: 'Payload Too Large',
    414: 'URI Too Long',
    415: 'Unsupported Media Type',
    416: 'Range Not Satisfiable',
    417: 'Expectation Failed',
    418: "I'm a Teapot",
    421: 'Misdirected Request',
    422: 'Unprocessable Entity',
    423: 'Locked',
    424: 'Failed Dependency',
    425: 'Too Early',
    426: 'Upgrade Required',
    428: 'Precondition Required',
    429: 'Too Many Requests',
    431: 'Request Header Fields Too Large',
    451: 'Unavailable For Legal Reasons',

    // 5xx - Server Error
    500: 'Internal Server Error',
    501: 'Not Implemented',
    502: 'Bad Gateway',
    503: 'Service Unavailable',
    504: 'Gateway Timeout',
    505: 'HTTP Version Not Supported',
    506: 'Variant Also Negotiates',
    507: 'Insufficient Storage',
    508: 'Loop Detected',
    510: 'Not Extended',
    511: 'Network Authentication Required'
} as Record<number, string>;

export const extractError = (rsp: any) => {
    const err = {
        title: 'Error: ' + rsp.status,
        description: undefined
    } as any;

    //If not able to get the status text, then try to get the error message from the response body.
    const contentType = rsp.headers.get('content-type') || '';
    if (contentType && rsp.data) {
        if (contentType.startsWith('text/html')) {
            const rawHtml = rsp.data;
            const parser = new DOMParser();
            const doc = parser.parseFromString(rawHtml, 'text/html');
            err.description = doc.body.textContent || 'Unknown error';
        } else if (contentType.startsWith('application/json')) {
            const errMessage = rsp.data;
            err.title = errMessage.title || 'Error: ' + rsp.status;
            err.description = errMessage.description || JSON.stringify(errMessage);
        } else {
            err.description = JSON.stringify(rsp.data);
        }
    }

    if (!err.description) {
        if (rsp.statusText) {
            err.description = `${rsp.status}: ${rsp.statusText}`;
        } else {
            // If not able to get the response content, then try to guess the status text from the status code
            err.description = HttpStatus[rsp.status];
        }
    }

    if (!err.description) {
        if (rsp.status >= 500 && rsp.status <= 599) {
            err.description = 'System error';
        } else if (rsp.status >= 400 && rsp.status <= 499) {
            err.description = 'User request error';
        } else {
            err.description = 'Unknown error';
        }
    }
    return err;
};
