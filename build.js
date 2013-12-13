var Template = require("C:\\Projects\\ion\\lib\\runtime\\Template.js");
var ast = {
    "op": "block",
    "args": [
        {
            "op": "var",
            "args": [
                "input",
                {
                    "op": "call",
                    "args": [
                        {
                            "op": "member",
                            "args": [
                                {
                                    "op": "var",
                                    "args": [
                                        "% 1",
                                        {
                                            "op": "output",
                                            "args": [
                                                0
                                            ]
                                        }
                                    ]
                                },
                                "getDirectory"
                            ]
                        },
                        {
                            "op": "ref",
                            "args": [
                                "% 1"
                            ]
                        },
                        "pages"
                    ]
                }
            ]
        },
        {
            "op": "var",
            "args": [
                "output",
                {
                    "op": "call",
                    "args": [
                        {
                            "op": "member",
                            "args": [
                                {
                                    "op": "var",
                                    "args": [
                                        "% 2",
                                        {
                                            "op": "output",
                                            "args": [
                                                0
                                            ]
                                        }
                                    ]
                                },
                                "getDirectory"
                            ]
                        },
                        {
                            "op": "ref",
                            "args": [
                                "% 2"
                            ]
                        },
                        "www/WEB-INF/pages"
                    ]
                }
            ]
        },
        {
            "op": "for",
            "args": [
                {
                    "op": "call",
                    "args": [
                        {
                            "op": "member",
                            "args": [
                                {
                                    "op": "var",
                                    "args": [
                                        "% 3",
                                        {
                                            "op": "ref",
                                            "args": [
                                                "input"
                                            ]
                                        }
                                    ]
                                },
                                "search"
                            ]
                        },
                        {
                            "op": "ref",
                            "args": [
                                "% 3"
                            ]
                        },
                        ".coffee"
                    ]
                },
                {
                    "op": "block",
                    "args": [
                        {
                            "op": "var",
                            "args": [
                                "source",
                                {
                                    "op": "input",
                                    "args": [
                                        0
                                    ]
                                }
                            ]
                        },
                        {
                            "op": "var",
                            "args": [
                                "target",
                                {
                                    "op": "call",
                                    "args": [
                                        {
                                            "op": "member",
                                            "args": [
                                                {
                                                    "op": "var",
                                                    "args": [
                                                        "% 4",
                                                        {
                                                            "op": "ref",
                                                            "args": [
                                                                "output"
                                                            ]
                                                        }
                                                    ]
                                                },
                                                "getFile"
                                            ]
                                        },
                                        {
                                            "op": "ref",
                                            "args": [
                                                "% 4"
                                            ]
                                        },
                                        {
                                            "op": "call",
                                            "args": [
                                                {
                                                    "op": "ref",
                                                    "args": [
                                                        "changeExtension"
                                                    ]
                                                },
                                                null,
                                                {
                                                    "op": "ref",
                                                    "args": [
                                                        "key"
                                                    ]
                                                },
                                                ".js"
                                            ]
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            "op": "set",
                            "args": [
                                {
                                    "op": "member",
                                    "args": [
                                        {
                                            "op": "ref",
                                            "args": [
                                                "target"
                                            ]
                                        },
                                        "path"
                                    ]
                                },
                                {
                                    "op": "+",
                                    "args": [
                                        "(function(){",
                                        {
                                            "op": "+",
                                            "args": [
                                                {
                                                    "op": "call",
                                                    "args": [
                                                        {
                                                            "op": "ref",
                                                            "args": [
                                                                "compileCoffeeScript"
                                                            ]
                                                        },
                                                        null,
                                                        {
                                                            "op": "ref",
                                                            "args": [
                                                                "source"
                                                            ]
                                                        }
                                                    ]
                                                },
                                                "})"
                                            ]
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],
    "id": "\r\nCompile coffeescript pages\r\n\r\n    input = @getDirectory 'pages'\r\n    output = @getDirectory 'www/WEB-INF/pages'\r\n    for input.search \".coffee\"\r\n        source = .\r\n        target = output.getFile changeExtension key, \".js\"\r\n        [target.path]: \"(function(){\" + compileCoffeeScript(source) + \"})\"\r\n\r\n"
};
module.exports = function(input, output, variables) {
    if (variables == null) variables = {};
    if (variables.module == null) variables.module = module;
    if (variables.require == null) variables.require = require;
    return new Template(ast, input, output, variables);
}