package butterknife.compiler;

/**
 * A field or method view binding.
 */
interface ViewBinding {
  /**
   * A description of the binding in human readable form (e.g., "field 'foo'").
   * 对于使用注解绑定对象的描述信息
   */
  String getDescription();
}
