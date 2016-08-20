gulp = require 'gulp'
jade = require 'gulp-pug'

gulp.task 'pug', =>
  gulp.src ['./pug/**/*.pug', '!./pug/**/_*.pug']
    .pipe jade({pretty: true})
    .pipe gulp.dest('./html/')
