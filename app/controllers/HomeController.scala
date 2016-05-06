package controllers

import javax.inject._

import play.api._
import play.api.libs.EventSource
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  val (chatOut, chatChannel) = Concurrent.broadcast[JsValue]

  def index = Action { implicit request =>
    Ok(views.html.index(routes.HomeController.chatFeed(), routes.HomeController.postMessage()))
  }

  def chatFeed = Action { request =>
    println("Someone has connected" + request.remoteAddress)
    Ok.chunked(chatOut
      &> EventSource()
    ).as("text/event-stream")
  }

  def postMessage = Action(parse.json) { request =>
    chatChannel.push(request.body)
    Ok
  }

}
