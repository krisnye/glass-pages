
// global function declarations for pages
(function(){

this.global = this;

this.write = function(text, escape) {
    if (text == null)
        return;

    text = String(text);
    if (escape)
        text = text.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');

    response.getWriter().print(text);
}

function log(args, type, target)
{
    var text = Array.prototype.join.call(args, ' ');
    java.lang.System[target].println(text);
}

this.console = {
    log: function(text) {
        log(arguments, "log", "out");
    },
    info: function(text) {
        log(arguments, "info", "out");
    },
    debug: function(text) {
        log(arguments, "debug", "out");
    },
    error: function(text) {
        log(arguments, "error", "err");
    },
    warn: function(text) {
        log(arguments, "warn", "err");
    }
}

})()
