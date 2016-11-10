module App.Messages exposing (Msg(..))

import Component.Messages
import Component.Model
import Http


type Msg
    = GetSystemStatus
    | GetFailed Http.Error
    | GetSucceeded (List Component.Model.Model)
    | ComponentMsg Component.Messages.Msg
