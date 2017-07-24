module Metric.Model exposing (Model, Status(..), compareStatus)


type alias Model =
    { key : String
    , status : Status
    , displayValue : String
    , numericValue : Int
    }


type Status
    = Info
    | Warn
    | Error
    | Missing


compareStatus : Status -> Status -> Order
compareStatus status1 status2 =
    let
        statusToInt s =
            case s of
                Info ->
                    0

                Warn ->
                    1

                Error ->
                    2

                Missing ->
                    3
    in
        compare (statusToInt status1) (statusToInt status2)
