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


initModel : Page -> Model
initModel page =
    Model Components.Model.init Detail.Model.init page


init : Navigation.Location -> ( Model, Cmd Msg )
init location =
    let
        page =
            hashParser location
    in
        ( initModel page, Navigation.newUrl <| toHash page )


main : Program Never Model Msg
main =
    Navigation.program (UpdateUrl << hashParser)
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        }
