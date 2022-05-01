package au.com.rsutton.navigation.router.md;

import org.junit.Test;

import au.com.rsutton.navigation.router.md.RoutePlanner3D.MoveTemplate;

public class RoutePlanner3DTest
{

	@Test
	public void test()
	{

		RoutePlanner3D planner = new RoutePlanner3D(100, 50, 72);

		MoveTemplate straight = planner.moveTemplateFactory(1, planner.angleFactory(0));
		MoveTemplate softRight = planner.moveTemplateFactory(5, planner.angleFactory(5));
		MoveTemplate softLeft = planner.moveTemplateFactory(5, planner.angleFactory(-5));
		MoveTemplate hardRight = planner.moveTemplateFactory(20, planner.angleFactory(10));
		MoveTemplate hardLeft = planner.moveTemplateFactory(20, planner.angleFactory(-10));
		MoveTemplate reverseLeft = planner.moveTemplateFactory(100, planner.angleFactory(170));
		MoveTemplate reverseRight = planner.moveTemplateFactory(100, planner.angleFactory(190));

		MoveTemplate[] moveTemplates = new MoveTemplate[] {
				straight, softRight, softLeft, hardRight, hardLeft, //
				// reverseLeft, reverseRight,

		};

		System.out.println("Start plan");
		planner.plan(20, 20, planner.angleFactory(90), moveTemplates);

		System.out.println("Start dump");

		moveTemplates = new MoveTemplate[] {
				straight, softRight, softLeft, // hardRight, hardLeft //
												// reverseLeft,
				// reverseRight,

		};
		planner.dumpFrom(30, 10, planner.angleFactory(270));

		// planner.dumpMap();
	}

}
