module App.Model exposing (Model, getSystemStatus)

import App.Messages
import App.Messages exposing (..)
import Components.Decoder
import Components.Model
import Nav.Model
import Http
import Task exposing (Task)


type alias Model =
    { components : Components.Model.Model
    , page : Nav.Model.Page
    }


getSystemStatus : Cmd Msg
getSystemStatus =
    Task.perform GetFailed GetSucceeded getSystemStatusRequest


getSystemStatusRequest : Task Http.Error Components.Model.Model
getSystemStatusRequest =
    Http.get Components.Decoder.decoder "/systemstatus.json"
