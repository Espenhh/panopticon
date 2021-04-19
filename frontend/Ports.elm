port module Ports exposing (..)


port login : () -> Cmd msg


port loginResult : (String -> msg) -> Sub msg


log : String -> Cmd msg
log message =
    logPort message


port logPort : String -> Cmd msg
