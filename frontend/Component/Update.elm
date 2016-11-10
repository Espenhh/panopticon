module Component.Update exposing (update)

import Component.Messages exposing (..)
import Component.Model exposing (..)


update : Msg -> Model -> Model
update msg model =
    case msg of
        Update ->
            model
