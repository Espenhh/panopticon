module Detail.Update exposing (update)

import Detail.Model exposing (..)
import Detail.Messages exposing (..)


update : Msg -> Model -> Model
update msg model =
    case msg of
        Update ->
            model

        Get (Err _) ->
            model

        Get (Ok metrics) ->
            metrics
