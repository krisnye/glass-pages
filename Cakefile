{utility,watcher} = builder = require 'glass-build'

serverConfig =
    name: "glass-pages-server"
    source:
        directory: 'coffee/server'
    node:
        directory: 'lib/server'
    browser:
        input: {}
        output:
            directory: 'war/WEB-INF/js'
            webroot: 'war'

clientConfig =
    name: "glass-pages"
    source:
        directory: 'coffee/client'
    node:
        directory: 'lib/client'
    browser:
        input: {}
        output:
            directory: 'war/js'
            webroot: 'war'

javaSource = "src"
isWindows = process.platform is 'win32'
ext = if isWindows then ".bat" else ""
server = null

start = ->
    server = utility.spawn "ant#{ext} runserver"

restart = ((callback) ->
    kill ->
        start()
        callback?()
    ).debounce(1000)

kill = (callback) ->
    server?.kill()
    server = null
    # in case that doesn't succeed, on windows
    # we will also do a task kill of all java.exe processes
    if isWindows
        utility.exec "taskkill /F /IM java.exe", callback
    else
        callback?()

task 'run', 'runs the development server', run = ->
    restart ->
        # watch for source changes and restart as needed.
        watcher.watchDirectory javaSource, {include:".java",initial:false}, (file) ->
            restart()
        builder.watch serverConfig
        builder.watch clientConfig

task 'kill', 'kills the development server', kill

task 'build', ->
    builder.build serverConfig, ->
        builder.build clientConfig, ->
            utility.spawn "ant#{ext} compile", ->

task 'test', ->
    builder.test serverConfig, ->
        builder.test clientConfig

task 'js', 'runs the rhino interpreter', ->
    utility.spawn "java -jar war/WEB-INF/lib/js.jar"
