package api;

import java.lang.reflect.Field;

import javassist.Modifier;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "localizedMessage", "cause", "stackTrace", "suppressed" })
public class RestStatus extends RuntimeException {

	private static final long serialVersionUID = -3866308675682764807L;

	public static final String CODE_OK = "0";

	public static final String CODE_ACCESS_UNAUTHORIZED = "1";

	public static final String CODE_NOT_FOUND = "2";

	public static final String CODE_ALREADY_EXISTS = "3";

	public static final String CODE_INTERNAL_SERVER_ERROR = "-1";

	public static final RestStatus OK = valueOf(CODE_OK);
	public static final RestStatus ACCESS_UNAUTHORIZED = valueOf(CODE_ACCESS_UNAUTHORIZED);
	public static final RestStatus NOT_FOUND = valueOf(CODE_NOT_FOUND);

	private String code;

	private String status;

	private String message;

	public RestStatus(String code, String status) {
		super(status);
		this.code = code;
		this.status = status;
	}

	public RestStatus(String code, String status, String message) {
		super(message);
		this.code = code;
		this.status = status;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public static RestStatus valueOf(String code) {
		String status = findStatus(code);
		return new RestStatus(code, status);
	}

	public static RestStatus valueOf(String code, String message) {
		if (StringUtils.isBlank(message))
			return valueOf(code);
		String status = findStatus(code);
		return new RestStatus(code, status, message);
	}

	private static String findStatus(String code) {
		try {
			for (Field f : RestStatus.class.getDeclaredFields())
				if (Modifier.isStatic(f.getModifiers())
						&& Modifier.isFinal(f.getModifiers())
						&& f.getType() == String.class
						&& f.get(null).equals(code)) {
					String status = f.getName();
					if (status.startsWith("CODE_"))
						status = status.substring(5);
					return status;
				}
		} catch (Exception e) {

		}
		return null;
	}

}
