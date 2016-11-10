module App.Subscriptions exposing (subscriptions)

import App.Model exposing (..)
import App.Messages exposing (..)
import Time exposing (second)


subscriptions : Model -> Sub Msg
subscriptions model =
    Time.every (10 * second) (\_ -> GetSystemStatus)
