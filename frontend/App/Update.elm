module App.Update exposing (update)

import App.Messages exposing (..)
import App.Model exposing (..)
import Components.Update


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        GetSystemStatus ->
            ( model, getSystemStatus )

        GetFailed error ->
            ( model, Cmd.none )

        GetSucceeded components ->
            ( { model | components = components }, Cmd.none )

        ComponentsMsg componentsMsg ->
            let
                componentsModel =
                    Components.Update.update componentsMsg model.components
            in
                ( { model | components = componentsModel }, Cmd.none )
