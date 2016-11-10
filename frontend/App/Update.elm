module App.Update exposing (update)

import App.Messages exposing (..)
import App.Model exposing (..)
import Component.Update


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        GetSystemStatus ->
            ( model, getSystemStatus )

        GetFailed error ->
            ( model, Cmd.none )

        GetSucceeded components ->
            ( { model | components = components }, Cmd.none )

        ComponentMsg componentMsg ->
            let
                componentModels =
                    List.map (Component.Update.update componentMsg) model.components
            in
                ( { model | components = componentModels }, Cmd.none )
