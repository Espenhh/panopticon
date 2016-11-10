module App.Model exposing (Model, getSystemStatus, init)

import App.Messages
import App.Messages exposing (..)
import Component.Decoder
import Component.Model
import Http
import Task exposing (Task)


type alias Model =
    { components : List Component.Model.Model
    }


init : ( Model, Cmd Msg )
init =
    ( Model [], getSystemStatus )


getSystemStatus : Cmd Msg
getSystemStatus =
    Task.perform GetFailed GetSucceeded getSystemStatusRequest


getSystemStatusRequest : Task Http.Error (List Component.Model.Model)
getSystemStatusRequest =
    Http.get Component.Decoder.listDecoder "/systemstatus.json"
