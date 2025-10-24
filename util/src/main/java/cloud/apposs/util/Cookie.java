package cloud.apposs.util;

public class Cookie {
    private final String name;

    private String value;

    private String domain;

    private String path;

    private long maxAge = -1;

    private boolean secure;

    private boolean httpOnly;

    public Cookie(String name, String value) {
        this.name = name.trim();
        setValue(value);
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public Cookie setValue(String value) {
        this.value = value;
        return this;
    }

    public String domain() {
        return domain;
    }

    public Cookie setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String path() {
        return path;
    }

    public Cookie setPath(String path) {
        this.path = path;
        return this;
    }

    public long maxAge() {
        return maxAge;
    }

    public Cookie setMaxAge(long maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public boolean isSecure() {
        return secure;
    }

    public Cookie setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public Cookie setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Cookie)) {
            return false;
        }

        Cookie that = (Cookie) o;
        if (!name().equals(that.name())) {
            return false;
        }

        if (path() == null) {
            if (that.path() != null) {
                return false;
            }
        } else if (that.path() == null) {
            return false;
        } else if (!path().equals(that.path())) {
            return false;
        }

        if (domain() == null) {
            if (that.domain() != null) {
                return false;
            }
        } else {
            return domain().equalsIgnoreCase(that.domain());
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append(name())
                .append('=')
                .append(value());
        if (domain() != null) {
            builder.append(", domain=").append(domain());
        }
        if (path() != null) {
            builder.append(", path=").append(path());
        }
        if (maxAge() >= 0) {
            builder.append(", maxAge=").append(maxAge()).append('s');
        }
        if (isSecure()) {
            builder.append(", secure");
        }
        if (isHttpOnly()) {
            builder.append(", HTTPOnly");
        }
        return builder.toString();
    }
}
