module Components.Update exposing (update)

import Components.Model exposing (..)
import Components.Messages exposing (..)
import Component.Update
import Component.Messages


update : Msg -> Model -> Model
update msg model =
    case msg of
        ComponentMsg componentMsg ->
            let
                environments =
                    List.map (\env -> updateComponents env componentMsg) model.environments
            in
                ({ model | environments = environments })


updateComponents : Environment -> Component.Messages.Msg -> Environment
updateComponents env msg =
    Components.Model.Environment env.name <| List.map (Component.Update.update msg) env.components
