module App.Messages exposing (Msg(..))

import Components.Messages
import Components.Model
import Detail.Messages
import Nav.Model exposing (..)
import Http


type Msg
    = UpdateUrl Page
    | GetSystemStatus
    | SystemStatus (Result Http.Error Components.Model.Model)
    | ComponentsMsg Components.Messages.Msg
    | DetailMsg Detail.Messages.Msg
