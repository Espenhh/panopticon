module Detail.Messages exposing (Msg(..))

import Http
import Detail.Model exposing (..)


type Msg
    = Update
    | GetFailed Http.Error
    | GetSucceeded Model
