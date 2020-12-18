package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ActorCapabilities.BankAccount.{Deposit, Statement, TransactionFailure, TransactionSuccess, Withdraw}
import part2actors.ActorCapabilities.Person.LiveTheLife

object ActorCapabilities extends App {

    class SimpleActor extends Actor {

      override def receive: Receive = {
        case "Hi!" => context.sender() ! "Hello, there!"
        case message: String => println(s"[${self}] I have received $message")
        case number: Int => println(s"[simple actor] I have received a NUMBER: $number")
        case SpecialMessage(contents) => println(s"[simple actor] I have received a something SPECIAL: $contents")
        case SendMessageToYourself(content) =>
          self ! content
        case SayHiTo(ref) => ref ! "Hi!" // alice is being passed as the sender
        case WirelessPhoneMessage(content, ref) => ref forward (content + "s")// I keep the original sender of the WPM
      }
    }
  val system = ActorSystem("actorCapabilititesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "hello, actor"

  // 1 - message can be of any type
  // a) messages must be IMMUTABLE
  // b)  message must be SERIALIZABLE
  // in practice use case classes and case objects

  simpleActor ! 42

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("some special content")

  // 2 - actors have information about their context and about themselves
  // context.self === this in OOP
  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor and I am proud of it")

  // 3 - actors can REPLY to meesages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)
  alice ! SayHiTo(bob)

  // 4 - dead letters
  alice ! "Hi!" // reply to "me"

  // 5 - forwading messages
  // D -> A -> B
  // forwading = sending a message with the ORIGINAL sender

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi", bob) // no sender

  /**
   *  Exercises
   *
   *  1. a Counter actor
   *  - Increment
   *  - Decrement
   *  - Print
   *
   *  2. a Back account as an actor
   *  - Deposit an amount
   *  - Withdraw an amout
   *  - Statement
   *  replies with
   *  - Success
   *  - Failure
   *
   *  interact with some other kind of an actor
   */
  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._

    var count = 0
    override def receive: Receive = {
      case Increment => count += 1
      case Decrement => count -= 1
      case Print => println(s"[counter] My current count is $count")
    }
  }
  import Counter._
  val counter = system.actorOf(Props[Counter], "myCounter")

  (1 to 5).foreach(_ => counter ! Increment)
  (1 to 3).foreach(_ => counter ! Decrement)
  counter ! Print

  object BankAccount {
    case class Deposit(amount: Int)
    case class Withdraw(amount: Int)
    case object Statement

    case class TransactionSuccess(message: String)
    case class TransactionFailure(reason: String)
  }

  // bank account
  class BankAccount extends Actor {
    var funds = 0
    override def receive: Receive = {
      case Deposit(amount) =>
        if (amount < 0) sender() ! TransactionFailure("invalid deposit amount")
        else {
          funds += amount
          sender() ! TransactionSuccess(s"Succesfully deposited $amount")
        }
      case Withdraw(amount) =>
        if (amount < 0 ) sender() ! TransactionFailure("invalid withdraw amount")
        else if ( amount > funds ) sender() ! TransactionFailure("insufficient funds")
        else {
           funds -= amount
          sender() ! TransactionSuccess(s"successfully withdrew $amount")
        }
      case Statement => sender() ! s"Your balance is $funds"
    }
  }
  object Person {
    case class LiveTheLife(account: ActorRef)
  }

  class Person extends Actor {
    import Person._

    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit (10000)
        account ! Withdraw (90000)
        account ! Withdraw (5000)
        account ! Statement
      case message => println(message.toString)
    }
  }

  val account = system.actorOf(Props[BankAccount], "bankAccount")
  val person = system.actorOf(Props[Person], "billionare")

  person ! LiveTheLife(account)
}
