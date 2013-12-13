fs = require 'fs'
cp = require 'child_process'

# create a symlink to the local ion project if present
if fs.existsSync '../ion/lib'
    try
        fs.mkdir 'node_modules' if not fs.existsSync 'node_modules'
        fs.symlinkSync '../../ion/lib', 'node_modules/ion', 'dir' if not fs.existsSync 'node_modules/ion'
        # also add a symlink from the WEB-INF/js to our node_modules folder
        fs.symlinkSync '../../node_modules', 'www/WEB-INF/js', 'dir' if not fs.existsSync 'www/WEB-INF/js'
    catch e
        console.log "You need to run as an administrator to create symlinks: #{e}"

task "watch", "watches and builds this project", ->
    console.log require('ion/builder').runTemplate 'build.ion'

task "kill", "kills java server on windows", ->
    cp.exec "taskkill /F /IM java.exe", (args...) ->
        console.log arg for arg in args when arg?