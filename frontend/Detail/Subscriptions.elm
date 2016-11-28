module Detail.Subscriptions exposing (subscriptions)

import Detail.Model exposing (..)
import Detail.Messages exposing (..)
import Time exposing (Time, second)


subscriptions : Model -> String -> Sub Msg
subscriptions model baseUrl =
    Time.every (10 * second) (\_ -> GetDetails baseUrl)
