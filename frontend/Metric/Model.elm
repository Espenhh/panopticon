module Metric.Model exposing (Model, Status(..))


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
