module Detail.Messages exposing (Msg(..))

import Detail.Model exposing (..)
import Http


type Msg
    = Update
    | GetDetails String
    | Get (Result Http.Error Model)
