module Metric.Model exposing (Model, Status(..))


type alias Model =
    { key : String
    , displayValue : String
    , numericValue : String
    , status : Status
    }


type Status
    = Info
    | Warn
    | Error
