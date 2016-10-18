gulp = require 'gulp'
jade = require 'gulp-pug'
coffee = require 'gulp-coffee'
plumber = require 'gulp-plumber'

gulp.task 'pug', ->
  gulp.src ['./pug/**/*.pug', '!./pug/**/_*.pug']
    .pipe plumber()
    .pipe jade({pretty: true})
    .pipe gulp.dest('./output/')

gulp.task 'coffee', ->
  gulp.src './coffee/**/*.coffee'
    .pipe plumber()
    .pipe coffee()
    .pipe gulp.dest('./output/js/')

gulp.task 'css', ->
  gulp.src './css/**'
    .pipe plumber()
    .pipe gulp.dest('./output/css/')

gulp.task 'copy', ->
  gulp.src './node_modules/bootstrap-material-design/dist/**'
    .pipe plumber()
    .pipe gulp.dest('./output/lib/bootstrap-material-design/')

gulp.task 'compile', ['pug', 'coffee', 'css', 'copy']

gulp.task 'watch', ['compile'], ->
  gulp.watch('./pug/**/*.pug', ['pug'])
  gulp.watch('./coffee/**/*.coffee', ['coffee'])
  gulp.watch('./css/**', ['css'])

gulp.task 'default', ['watch']
