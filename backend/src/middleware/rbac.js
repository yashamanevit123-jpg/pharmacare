/**
 * Role-Based Access Control (RBAC) middleware factory
 * @param  {...string} allowedRoles - Names of allowed roles (e.g. 'Administrator', 'Pharmacist')
 */
function authorizeRoles(...allowedRoles) {
  return (req, res, next) => {
    if (!req.user) {
      return res.status(401).json({
        success: false,
        error: 'Authentication required prior to authorization.',
      });
    }

    if (!allowedRoles.includes(req.user.roleName)) {
      return res.status(403).json({
        success: false,
        error: `Access Denied: Role '${req.user.roleName}' is unauthorized for this resource. Required: [${allowedRoles.join(', ')}].`,
      });
    }

    next();
  };
}

module.exports = {
  authorizeRoles,
};
