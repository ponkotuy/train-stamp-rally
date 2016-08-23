gulp = require 'gulp'
jade = require 'gulp-pug'
coffee = require 'gulp-coffee'

gulp.task 'pug', =>
  gulp.src ['./pug/**/*.pug', '!./pug/**/_*.pug']
    .pipe jade({pretty: true})
    .pipe gulp.dest('./output/')

gulp.task 'coffee', =>
  gulp.src './coffee/**/*.coffee'
    .pipe coffee()
    .pipe gulp.dest('./output/js/')

gulp.task 'compile', ['pug', 'coffee']

gulp.task 'watch', =>
  gulp.watch('./pug/**/*.pug', ['pug'])
  gulp.watch('./coffee/**/*.coffee', ['coffee'])

gulp.task 'default', ['compile', 'watch']
