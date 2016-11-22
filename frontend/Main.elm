module Main exposing (..)

import Navigation
import App.Subscriptions exposing (subscriptions)
import App.Messages exposing (..)
import App.Model exposing (..)
import App.Update exposing (update)
import App.View exposing (view)
import Components.Model
import Detail.Model
import Nav.Nav exposing (hashParser, toHash)
import Nav.Model exposing (Page)


initModel : Flags -> Page -> Model
initModel flags page =
    Model flags Components.Model.init Detail.Model.init page


init : Flags -> Navigation.Location -> ( Model, Cmd Msg )
init flags location =
    let
        page =
            hashParser location
    in
        ( initModel flags page, Navigation.newUrl <| toHash page )


main : Program Flags Model Msg
main =
    Navigation.programWithFlags (UpdateUrl << hashParser)
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        }
