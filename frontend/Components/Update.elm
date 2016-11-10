module Components.Update exposing (update)

import Components.Model exposing (..)
import Components.Messages exposing (..)
import Component.Update


update : Msg -> Model -> Model
update msg model =
    case msg of
        ComponentMsg componentMsg ->
            let
                componentModels =
                    List.map (Component.Update.update componentMsg) model.components
            in
                ({ model | components = componentModels })
