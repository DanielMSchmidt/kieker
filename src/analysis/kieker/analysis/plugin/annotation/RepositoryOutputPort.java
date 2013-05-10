package kieker.analysis.plugin.annotation;

public @interface RepositoryOutputPort {

	/**
	 * The human-readable description of this port.
	 * 
	 * @return The description for this port.
	 */
	String description() default "Repository Output Port";

	/**
	 * The name which is used to identify this port. It should be unique within the class.
	 * 
	 * @return The name of this port.
	 */
	String name();

	/**
	 * The event types which are used for this port. If this is empty, everything can be sent through the port.
	 * 
	 * @return The event types for this class.
	 */
	Class<?>[] eventTypes() default {};

}