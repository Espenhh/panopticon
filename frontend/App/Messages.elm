module App.Messages exposing (Msg(..))

import Components.Messages
import Components.Model
import Http


type Msg
    = GetSystemStatus
    | GetFailed Http.Error
    | GetSucceeded Components.Model.Model
    | ComponentsMsg Components.Messages.Msg
