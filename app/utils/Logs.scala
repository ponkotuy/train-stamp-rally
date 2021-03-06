package utils

import org.slf4j.{Logger, LoggerFactory}

trait Logs {
  protected val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  def trace(ex: Throwable) = if (logger.isTraceEnabled) logger.trace(ex.toString, ex)
  def trace(message: => String) = if (logger.isTraceEnabled) logger.trace(message)
  def trace(message: => String, ex: Throwable) = if (logger.isTraceEnabled) logger.trace(message, ex)

  def debug(message: => String) = if (logger.isDebugEnabled) logger.debug(message)
  def debug(message: => String, ex: Throwable) = if (logger.isDebugEnabled) logger.debug(message, ex)
  def debugValue[T](valueName: String, value: => T): T = {
    val result: T = value
    debug(valueName + " == " + result.toString)
    result
  }

  def info(ex: Throwable) = if (logger.isInfoEnabled) logger.info(ex.toString, ex)
  def info(message: => String) = if (logger.isInfoEnabled) logger.info(message)
  def info(message: => String, ex: Throwable) = if (logger.isInfoEnabled) logger.info(message, ex)

  def warn(ex: Throwable) = if (logger.isWarnEnabled) logger.warn(ex.toString, ex)
  def warn(message: => String) = if (logger.isWarnEnabled) logger.warn(message)
  def warn(message: => String, ex: Throwable) = if (logger.isWarnEnabled) logger.warn(message, ex)

  def error(ex: Throwable) = if (logger.isErrorEnabled) logger.error(ex.toString, ex)
  def error(message: => String) = if (logger.isErrorEnabled) logger.error(message)
  def error(message: => String, ex: Throwable) = if (logger.isErrorEnabled) logger.error(message, ex)
}
