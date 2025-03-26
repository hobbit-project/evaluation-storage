if [ -z $COMPONENT_LOG_LEVEL ]
then
    echo "Logging does not have to be adjusted."
else
    EXPRESSION='s/log4j.rootLogger=info/log4j.rootLogger='$COMPONENT_LOG_LEVEL'/g'
    echo "Adjusting logging by running:"
    echo $EXPRESSION
    sed -i $EXPRESSION log4j.properties
fi