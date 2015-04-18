/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.exceptions;

/**
 * For exceptions that actually mean, that operation made no sense in given context.
 * <p>
 * @author maartyl
 */
public class InvalidOperationException extends UnsupportedOperationException {

  public InvalidOperationException() {
  }

  public InvalidOperationException(String message) {
    super(message);
  }

  public InvalidOperationException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidOperationException(Throwable cause) {
    super(cause);
  }

//  public InvalidOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//    super(message, cause, enableSuppression, writableStackTrace);
//  }

}
