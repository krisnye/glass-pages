{utility,watcher} = builder = require 'glass-build'

serverConfig =
    name: "glass-pages-server"
    source:
        directory: 'src/server'
    node:
        directory: 'lib/server'
    browser:
        input:
            "glass-pages-server": "lib/server"
            "glass-test": true
        output:
            directory: 'war/WEB-INF/js'
            webroot: 'war'
            test: 'glass-test'

clientConfig =
    name: "glass-pages"
    source:
        directory: 'src/client'
    node:
        directory: 'lib/client'
    browser:
        input:
            "glass-pages": "lib/client"
            "glass-test": true
        output:
            directory: 'war/js'
            webroot: 'war'
            test: 'glass-test'
    appengine:
        java: 'java'
        pages: true

task 'build', ->
    builder.build serverConfig, ->
        builder.build clientConfig

task 'watch', 'runs dev server and watches for changes', run = ->
    builder.watch serverConfig
    builder.watch clientConfig
    require('fs').watchFile "war/WEB-INF/js/PageServlet.js", ->
        # rebuild the dist jar
        utility.spawn "ant.bat compile"

task 'kill', 'kills the development server', kill = ->
    builder.kill serverConfig
    builder.kill clientConfig

task 'test', ->
    builder.test serverConfig, ->
        builder.test clientConfig

task 'js', 'runs the rhino interpreter', ->
    utility.spawn "java -jar war/WEB-INF/lib/js.jar"
