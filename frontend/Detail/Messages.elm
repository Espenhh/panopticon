module Detail.Messages exposing (Msg(..))

import Http
import Detail.Model exposing (..)


type Msg
    = Update
    | Get (Result Http.Error Model)
