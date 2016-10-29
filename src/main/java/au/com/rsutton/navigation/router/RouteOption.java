package au.com.rsutton.navigation.router;

public enum RouteOption
{
	ROUTE_THROUGH_UNEXPLORED()
	{
		@Override
		public boolean isPointRoutable(double d)
		{
			return d < 0.55;
		}
	},
	ROUTE_THROUGH_CLEAR_SPACE_ONLY()
	{
		@Override
		public boolean isPointRoutable(double d)
		{
			return d < 0.45;
		}
	};

	abstract public boolean isPointRoutable(double d);
}
