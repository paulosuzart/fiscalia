fs          = require 'fs'
path        = require 'path'

{print} = require 'util'
{spawn, exec} = require 'child_process'


appFiles  = [
  'fiscalia'
].map (f) -> "app/#{f}.coffee"

build = (callback) ->
  appContents = new Array remaining = appFiles.length
  for file, index in appFiles then do (file, index) ->
    fs.readFile file, 'utf8', (err, fileContents) ->
      return logerr err if err
      appContents[index] = fileContents
      link() if --remaining is 0

  link = ->
    fs.writeFile 'cbuild/fiscalia.coffee', appContents.join('\n\n'), 'utf8', (err) ->
      return logerr err if err
      exec "coffee --compile -o public/js cbuild", (err, stdout, stderr) ->
        return logerr err if err
        console.log stdout + stderr
        fs.unlink 'cbuild/fiscalia.coffee', (err) ->
          return logerr err if err
          console.log 'Built.\n'

task 'build', 'Build lib/ from src/', ->
  build()

task 'watch', 'Watch src/ for changes', ->
    coffee = spawn 'coffee', ['-w', '-c', '-o', 'public/js', 'app']
    coffee.stderr.on 'data', (data) ->
      process.stderr.write data.toString()
    coffee.stdout.on 'data', (data) ->
      print data.toString()  

logerr = (err) ->
    console.log err.message + '\n'